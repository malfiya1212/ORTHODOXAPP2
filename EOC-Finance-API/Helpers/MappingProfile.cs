using AutoMapper;
using EOC.Finance.API.Models;
using EOC.Finance.API.DTOs;

namespace EOC.Finance.API.Helpers
{
    public class MappingProfile : Profile
    {
        public MappingProfile()
        {
            // User Mapping: Database -> DTO
            CreateMap<User, UserDto>()
                .ForMember(dest => dest.ChurchName, opt => opt.MapFrom(src => src.Church != null ? src.Church.Name : null))
                .ForMember(dest => dest.DioceseName, opt => opt.MapFrom(src => src.Diocese != null ? src.Diocese.Name : null));

            // Income Mapping: Database -> DTO
            CreateMap<Income, IncomeDto>();

            // Expense Mapping: Database -> DTO
            CreateMap<Expense, ExpenseDto>();

            // Organization Mappings
            CreateMap<Church, ChurchDto>();
            CreateMap<Diocese, DioceseDto>();
        }
    }
}
